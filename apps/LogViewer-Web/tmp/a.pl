#!/usr/env/perl
use strict;
use warnings;
use LogViewer::Schema;
my $schema = LogViewer::Schema->connect('dbi:SQLite:../../db/sample.db');
my @logs = $schema->resultset('Log')->search({
    created_on => { '>', 1292035981 }
});
foreach my $log (@logs) {
    print $log->nickname . ': ' . $log->message, "\n";
}

# 1292003760
# 1292035981
