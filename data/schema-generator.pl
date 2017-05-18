#!/usr/bin/perl

use strict;
use warnings;

use File::Find::Rule            qw();
use XML::LibXML                 qw();
use XML::LibXML::XPathContext   qw();

my %NS = (
            ts => 'http://www.sun.com/japex/testSuite',
            xi => 'http://www.w3.org/2001/XInclude',
         );

# list all test case configs
my $allXC = XML::LibXML::XPathContext->new( XML::LibXML->new->parse_file('../config/testCases/all.xml') );
$allXC->registerNs($_ => $NS{$_}) for keys %NS;

# process each in turn
for my $tcc ($allXC->findnodes('//xi:include')) {
    $tcc = '../config/testCases/' . $tcc->getAttributeNS(undef, 'href');
    my $xc = XML::LibXML::XPathContext->new( XML::LibXML->new->parse_file($tcc) );
    $xc->registerNs($_ => $NS{$_}) for keys %NS;
    
    # skip if schema
    next if $xc->findnodes('//ts:param[@name = "org.w3c.exi.ttf.schemaLocation"]');
    
    print "Handling $tcc...\n";
    my $path = makeSchema($xc);
    
    # add the element to the config, and save it back
    my $doc = $xc->getContextNode;
    my $prm = $doc->createElementNS($NS{ts}, 'param');
    $prm->setAttributeNS(undef, 'name', 'org.w3c.exi.ttf.schemaLocation');
    $prm->setAttributeNS(undef, 'value', "\${testsDir}/$path");
    my $prev = ($xc->findnodes('//ts:param[@name = "testsDir"]'))[0];
    $doc->documentElement->insertBefore($prm, $prev->nextSibling);
    $doc->documentElement->insertBefore($doc->createTextNode("\n  "), $prm);
    $doc->toFile($tcc);
    print "DONE\n";
}

# the gnomes that build schemata in the dark of the night
sub makeSchema {
    my $xc = shift;
    
    # one path to find them all
    my @files;
    for my $test ($xc->findnodes('//ts:testCase/ts:param[@name = "japex.inputFile"]')) {
        my $file = $test->getAttributeNS(undef, 'value');
        $file =~ s|^\${testsDir}/||;
        push @files, $file;
    }
    $files[0] =~ m{^([^/]+)};
    my $dir = $1;
    print "  - found " . scalar(@files) . " for $dir\n";
    
    # and now for some quick parsing (alas, all SAX parsers run out of file handles...)
    my %ns;
    for my $file (@files) {
        my $doc = XML::LibXML->new->parse_file($file);
        $ns{ ($doc->documentElement->namespaceURI || '') }{ $doc->documentElement->localName } = 1;
    }
    print "  - done parsing the files\n";

    # munge, munge
    my @schemata;
    my $i = 0;
    for my $ns (keys %ns) {
        push @schemata, {
                            name    => $i ? "autoschema_${i}.xsd" : 'autoschema.xsd',
                            ns      => $ns,
                            ln      => [ keys %{$ns{$ns}}],
                        };
        $i++;
    }
    
    # and he bethrotheth thine instance to the darkness of schemata
    for my $schema (@schemata) {
        open OUT, '>:utf8', "$dir/$schema->{name}" or die "Can't open $schema->{name}: $!";
        print OUT qq{<schema xmlns="http://www.w3.org/2001/XMLSchema"\n};
        print OUT qq{        targetNamespace="$schema->{ns}"\n} if $schema->{ns};
        print OUT qq{        elementFormDefault="qualified">\n\n};
        
        # if the first, write the imports
        if ($schema->{name} eq $schemata[0]->{name}) {
            for (my $i = 1; $i <= $#schemata; $i++) {
                print OUT "  <import namespace='$schemata[$i]->{ns}' schemaLocation='$schemata[$i]->{name}'/>\n";
            }
            print OUT "\n";
        }

        # for each ln, write element
        print OUT "  <element name='$_'/>\n" for @{$schema->{ln}};
        print OUT "</schema>\n";
        close OUT;
    }
    print "  - wrote " . scalar(@schemata) . " schema(ta) for $dir\n";
    
    return "$dir/autoschema.xsd";
}
