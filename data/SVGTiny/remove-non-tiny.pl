#!/usr/bin/perl

###
# remove-non-tiny.pl - Locates and removes SVG documents that are not Tiny
# Robin Berjon <robin.berjon@expway.fr>
# $Id: remove-non-tiny.pl,v 1.3 2006/05/16 13:25:44 rberjon Exp $
#--
# Put script at the root of a document hierarchy that contains SVG documents.
# It will locate them and based on a number of simple rules it will eliminate
# those that are not Tiny documents. It doesn't currently support svgz, but
# it may in the future.
# --
# This program is licenced under the W3C Licence
###

use strict;
use warnings;

use XML::LibXML                 qw();
use XML::LibXML::XPathContext   qw();

use File::Find::Rule    qw();
use File::Slurp         qw(read_file write_file);

# useful
use constant TEST   => 0;
use constant SVG    => "http://www.w3.org/2000/svg";
use constant XLINK  => "http://www.w3.org/1999/xlink";

sub Ex::new {
    shift;
    my $str = shift;
    return bless \$str, "Ex";
}

# useless
my $time = time();

# find stuff
my @files = File::Find::Rule->file()
                            ->name("*.svg")
                            ->in(".");

# and delete stuff
my @delete;
my $kept;
my $parser = XML::LibXML->new;
$parser->load_ext_dtd(0);
my $xc = XML::LibXML::XPathContext->new;
$xc->registerNs('svg', SVG);
$xc->registerNs('xlink', XLINK);
@files = @files[0..99] if TEST;

my $i = 0;
for my $file (@files) {
    $i++;
    my $modified = 0;
    eval {
        # DOCTYPEs will burn in hell
        my $content = read_file($file);
        $content =~ s{<!DOCTYPE svg PUBLIC [^\[]+?>}{}sm;
        $content =~ s{PUBLIC .*?\[}{\[}sm;

        # rule zero: this too shall parse
        my $doc = $parser->parse_string($content);
        $doc->setEncoding('UTF-8');
        $xc->setContextNode($doc);
        
        # rule one: it must be SVG
        my $root = $doc->documentElement;
        die Ex->new("Not SVG") unless defined($root->namespaceURI) and
                             $root->namespaceURI eq SVG   and 
                             $root->localName eq "svg";
        

        # rule two: some common elements that aren't in Tiny
        die Ex->new("Element not in Tiny") if $xc->findnodes('//svg:filter | //svg:clipPath | //svg:mask | //svg:marker |
                                                              //svg:pattern | //svg:style | /svg:svg//svg:svg | //svg:symbol |
                                                              //svg:textPath');

        # rule three: no xlink:href on gradients
        die Ex->new("XLink on gradient") if $xc->findnodes('//svg:linearGradient[@xlink:href] | //svg:linearGradient[@xlink:href]');

        # rule four: don't use the style attribute, stupid
        # this rule kills almost all content in OCAL, so we fix it
        # die Ex->new("Usage of style attribute") if $xc->findnodes('//svg:*/@style');
        for my $broken ($xc->findnodes('//svg:*[@style]')) {
            $modified = 1;
            my @props = split /\s*;\s*/, $broken->getAttributeNS(undef, 'style');
            for my $prop (@props) {
                next if $prop =~ m/^\s*$/;
                my ($k, $v) = split /\s*:\s*/, $prop;
                $broken->setAttributeNS(undef, $k, $v);
                $broken->removeAttributeNS(undef, 'style');
            }
        }

        # rule five: some properties that aren't in Tiny
        die Ex->new("Attribute not in Tiny") if $xc->findnodes('//svg:*[(@opacity and not(local-name() = "image")) or @class]');

        # write back modified files
        if ($modified) {
            $doc->toFile($file);
        }
    };
    push(@delete, "$file [" . ${$@} ."]\n") if $@;
    warn "Processed $i documents out of " . scalar(@files) . " (killed " . scalar(@delete) . ") in " . 
         (time() - $time) . " seconds...\n" unless $i > 1 and $i % 100;
}

write_file("deleted.txt", @delete);

# statistics
print "[DONE] Processed " . scalar(@files) . " documents (killed " . scalar(@delete) . ") in " . (time() - $time) . " seconds\n";
