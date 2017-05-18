#!/usr/bin/perl

use strict;
use warnings;
use Data::Dumper    qw(Dumper);

my $file = 'deleted.txt';
open DEL, "<$file" or die "Can't open $file: $!";
my %reasons;
while (my $line = <DEL>) {
    chomp $line;
    $line =~ m/(.+) \[(.+)\]/;
    my ($f, $msg) = ($1, $2);
    $reasons{$msg}++;
    unlink $f;
}
print Dumper(\%reasons);
close DEL;
