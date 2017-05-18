#!/usr/bin/perl

use strict;
use warnings;

use File::Find::Rule    qw();

# this is the list of directories
my @missing = qw/AVCL DAML MAGE-ML MeSH NLM RosettaNet XAL/; # WAP
# after this be dragons

for my $dir (@missing) {
    my @files = File::Find::Rule->file()
                                ->name(qw/*.xml.gz *.xml *.daml *.xml.zip *.xmi *.probe *.xal *.xdxf *.params *.tim/)
                                ->in($dir);
    my $conf = lc $dir;
    $conf = "../config/testCases/$conf.xml";
    die "Configuration file '$conf' already exists, won't overwrite\n" if -e $conf;
    open OUT, '>:utf8', $conf or die $!;
    print OUT <<'    EOXML';
    <testCaseGroup xmlns="http://www.sun.com/japex/testSuite">
      <param name="testsDir" value="${japex.exi.ttfms.testCasesDir}"/>

    EOXML
    for my $file (@files) {
        print OUT <<"        EOXML";
      <testCase name="$file">
        <param name="japex.inputFile" value="\${testsDir}/$file"/>
      </testCase>
        EOXML
    }
    print OUT "</testCaseGroup>\n";
    close OUT;
}
