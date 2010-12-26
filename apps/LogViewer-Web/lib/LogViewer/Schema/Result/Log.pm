package LogViewer::Schema::Result::Log;

# Created by DBIx::Class::Schema::Loader
# DO NOT MODIFY THE FIRST PART OF THIS FILE

use strict;
use warnings;

use Moose;
use MooseX::NonMoose;
use namespace::autoclean;
extends 'DBIx::Class::Core';

__PACKAGE__->load_components("InflateColumn::DateTime");

=head1 NAME

LogViewer::Schema::Result::Log

=cut

__PACKAGE__->table("log");

=head1 ACCESSORS

=head2 _id

  data_type: 'integer auto_increment'
  is_nullable: 1

=head2 channel

  data_type: 'text'
  is_nullable: 0

=head2 nickname

  data_type: 'text'
  is_nullable: 1

=head2 username

  data_type: 'text'
  is_nullable: 1

=head2 hostname

  data_type: 'text'
  is_nullable: 1

=head2 message

  data_type: 'text'
  is_nullable: 1

=head2 created_on

  data_type: 'integer'
  is_nullable: 0

=cut

__PACKAGE__->add_columns(
  "_id",
  { data_type => "integer auto_increment", is_nullable => 1 },
  "channel",
  { data_type => "text", is_nullable => 0 },
  "nickname",
  { data_type => "text", is_nullable => 1 },
  "username",
  { data_type => "text", is_nullable => 1 },
  "hostname",
  { data_type => "text", is_nullable => 1 },
  "message",
  { data_type => "text", is_nullable => 1 },
  "created_on",
  { data_type => "integer", is_nullable => 0 },
);


# Created by DBIx::Class::Schema::Loader v0.07002 @ 2010-12-26 14:38:34
# DO NOT MODIFY THIS OR ANYTHING ABOVE! md5sum:ZJVghQi/eoRvfAhLXdXRYg


# You can replace this text with custom content, and it will be preserved on regeneration
__PACKAGE__->meta->make_immutable;
1;
