#!/usr/bin/perl

use strict;
use warnings;

use File::Find::Rule            qw();
use XML::LibXML                 qw();
use XML::LibXML::XPathContext   qw();
use MIME::Base64                qw(encode_base64);

# names, in spaaaace
my %NS = (
    ds  => 'http://www.sun.com/xml/datastore',
);


# your typical configuration, only longer
my $dict = '/usr/share/dict/words';
my $binRoot = '/Users/robin/Desktop/stuff';
my @binExt = qw(*.jpg *.pdf *.gif *.oo3 *.tbz *.svg);
my %process = (
#    DataStore   => {
#            root    => 'DataStore/instance',
#            match   => '*.xml',
#            rules   => [
#                '//ds:stringT'  => [qw(words    2 5)],
#                '//ds:integerT' => [qw(integer  -999999 999999)],
#                '//ds:binaryT'  => [qw(binary   50 100)],
#                '//ds:booleanT' => [qw(boolean)],
#            ],
#    },
    Invoice     => {
            root    => 'Invoice/instance',
            match   => '*.xml',
            rules   => [
                '//LineID'                  => [qw(@words   schemeName 1 1)],
                '//LineID'                  => [qw(@words   schemeAgencyName 1 1)],
                '//LineID'                  => [qw(integer  0 10000)],
                '//StandardItemIdentifier'  => [qw(@words   schemeName 1 1)],
                '//StandardItemIdentifier'  => [qw(@words   schemeAgencyName 1 1)],
                '//StandardItemIdentifier'  => [qw(integer  0 100)],
                '//Description'             => [qw(string   30 60)],
                '//Quantity'                => [qw(integer  0 100)],
                '//OrderStatus'             => [qw(@words   listAgencyId 1 1)],
                '//OrderStatus'             => [qw(words    1 1)],
                '//ServiceID'               => [qw(@words   schemeName 1 1)],
                '//ServiceID'               => [qw(@words   schemeAgencyName 1 1)],
                '//ServiceID'               => [qw(words    1 1)],
                '//ConditionID'             => [qw(@words   schemeName 1 1)],
                '//ConditionID'             => [qw(@words   schemeAgencyName 1 1)],
                '//ConditionID'             => [qw(words    1 1)],
                '//GrossUnitPriceAmount'    => [qw(@currency currencyId)],
                '//GrossUnitPriceAmount'    => [qw(price    50 20000)],
                '//NetUnitPriceAmount'      => [qw(@currency currencyId)],
                '//NetUnitPriceAmount'      => [qw(price    50 20000)],
                '//Rate'                    => [qw(price    2 50)],
                '//TotalAmount'             => [qw(@currency currencyId)],
                '//TotalAmount'             => [qw(price    1000 80000)],
            ],
    },

);
# And now, chaos

my @words;
sub loadWords {
    return if @words;
    open my $FH, '<', $dict or die "Can't open dict file: $!";
    @words = map { chomp; $_ } <$FH>;
    close $FH;
    warn ">>>>> loaded " . scalar(@words) . " words\n";
}

my @bins;
sub loadBinaries {
    return if @bins;
    @bins = File::Find::Rule->file()
                            ->name(@binExt)
                            ->in($binRoot);
    warn ">>>>> loaded " . scalar(@bins) . " files\n";
}

sub rndWord {
    loadWords;
    return $words[rndInteger(0, $#words - 1)];
}

sub rndFile {
    loadBinaries;
    return $bins[rndInteger(0, $#bins - 1)];
}

my @curs = qw(EUR USD GBP AUD CAD ARS CNY DKK ISK INR IRR JPY NZD NOK);
sub rndCurrency {
    return $curs[rndInteger(0, $#curs - 1)];
}

sub rndPrice {
    my $int = rndInteger(@_);
    return $int . '.' . sprintf '%02u', rndInteger(0, 99);
}

sub rndString {
    my $size = rndInteger(@_);
    my $str = '';
    $str .= rndWord while length $str < $size;
    return $str;
}

sub rndWords {
    my $num = rndInteger(@_);
    my @tmp;
    push @tmp, rndWord for (1..$num);
    return join ' ', @tmp;
}

sub rndInteger {
    my ($min, $max) = @_;
    return int(rand( 1 + $max - $min )) + $min;
}

sub rndBinary {
    my $len = rndInteger(@_);
    my $file = rndFile;
    my $size = -s $file;
    my $offset = rndInteger(0, ($size - $len > 0) ? $size - $len : 0);
    my $bytes;
    open my $FH, '<:raw', $file or die "Can't open binary file '$file': $!";
    my $got = read $FH, $bytes, $len, $offset;
    close $FH;
    $bytes =~ s/\0//gsm; # strange abundance of nulls
    #warn "@@ [BIN]: len=$len, off=$offset, size=$size, got=$got\n";
    #warn "@@ <<" . encode_base64($bytes) . ">>\n";
    return encode_base64 $bytes;
}

sub rndBoolean {
    return [qw(true false 0 1)]->[rndInteger(0, 3)]
}

my %table = (
    words   => \&rndWords,
    string  => \&rndString,
    integer => \&rndInteger,
    binary  => \&rndBinary,
    boolean => \&rndBoolean,
    currency =>\&rndCurrency,
    price   => \&rndPrice,
);

while (my ($k,$v) = each %process) {
    warn "[Processing $k]\n";
    my @files = File::Find::Rule->file()
                                ->name($v->{match})
                                ->in($v->{root});
    for my $file (@files) {
        warn "\t- $file\n";
        my $doc = XML::LibXML->new->parse_file($file);
        my $xc = XML::LibXML::XPathContext->new($doc);
        $xc->registerNs($_, $NS{$_}) for keys %NS;
        my @rules = @{$v->{rules}};
        while (@rules) {
            my $rule = shift @rules;
            my $do = shift @rules;
            for my $el ($xc->findnodes($rule)) {
                my @do = @$do;
                my $attr = 0;
                my $aName;
                my $rule = shift @do;
                if ($rule =~ s/^@//) {
                    $attr = 1;
                    $aName = shift @do;
                }
                my $value = $table{$rule}->(@do);
                if ($attr) {
                    $el->setAttributeNS(undef, $aName, $value);
                }
                else {
                    $el->removeChild($el->firstChild) while $el->firstChild;
                    $el->appendChild($doc->createTextNode($value));
                }
            }
        }
        $doc->toFile($file);
    }
}

