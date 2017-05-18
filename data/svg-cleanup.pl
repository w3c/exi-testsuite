#!/usr/bin/perl
# extracted from stidy

use strict;
use warnings;

use File::Find::Rule            qw();
use File::Path                  qw(mkpath);
use XML::LibXML                 qw();
use XML::LibXML::XPathContext   qw();

my %NS = (
            ts  => 'http://www.sun.com/japex/testSuite',
            xi  => 'http://www.w3.org/2001/XInclude',
            svg => 'http://www.w3.org/2000/svg',
            cc  => 'http://web.resource.org/cc/',
            dc  => 'http://purl.org/dc/elements/1.1/',
            ink => 'http://www.inkscape.org/namespaces/inkscape',
            rdf => 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
            sp  => 'http://inkscape.sourceforge.net/DTD/sodipodi-0.dtd',
);

# list all test case configs
my $allXC = XML::LibXML::XPathContext->new( XML::LibXML->new->parse_file('../config/testCases/svgtiny.xml') );
$allXC->registerNs($_ => $NS{$_}) for keys %NS;

my %delAt = (
    ink => [qw(version export-filename export-xdpi export-ydpi)],
    sp  => [qw(docbase docname version)],
);

for my $svgIn ($allXC->findnodes('//ts:testCase')) {
    my $in = $svgIn->getAttributeNS(undef, 'name');
    my $sxc = XML::LibXML::XPathContext->new( XML::LibXML->new->parse_file($in) );
    $sxc->registerNs($_ => $NS{$_}) for keys %NS;

    # fix it (taking stupid libxml bugs into account)
    $_->parentNode->removeChild($_) for $sxc->findnodes('//svg:metadata | //sp:*');
    for my $el ($sxc->findnodes('//*[@ink:* or @sp:*]')) {
        for my $ns (keys %delAt) {
            for my $ln (@{$delAt{$ns}}) {
                $el->removeAttributeNS($NS{$ns}, $ln) if $el->hasAttributeNS($NS{$ns}, $ln);
            }
        }
    }
    
    # decrease precision on paths
    for my $path ($sxc->findnodes('//svg:path')) {
        # this is brutal
        my $d = $path->getAttributeNS(undef, 'd');
        $d =~ s/(\.\d)\d*/$1/g;
        $path->setAttributeNS(undef, 'd', $d);
    }
    

    # make output path
    my $out = $in;
    $out =~ s/^SVGTiny/SVGTinyCleaned/;
    my $path = $out;
    $path =~ s{[^/]*$}{};
    mkpath($path);
    $sxc->getContextNode->toFile($out);
    warn "File '$in' done.\n";
}