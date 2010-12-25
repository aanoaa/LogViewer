use AnyEvent::DBI;

my $dbh = new AnyEvent::DBI "DBI:SQLite:dbname=sample.db", "", "";

$dbh->exec ("select * from test where num=?", 10, sub {
    my ($dbh, $rows, $rv) = @_;
    $#_ or die "failure: $@";

    print "@$_\n"
    for @$rows;

    $cv->broadcast;
});

# asynchronously do sth. else here

$cv->wait;


my $instance = AnyEvent::DBI ""
my $dbh = $self->get_dbh();
my $sth = $dbh->exec("INSERT INTO log (channel, nickname, username, hostname, message, created_on) VALUES (?, ?, ?, ?, ?, ?)",
        $message->channel,
        $message->from->nickname,
        $message->from->username,
        $message->from->hostname,
        $message->message,
        time(),
        \&Morris::_noop_cb,
        ); 
