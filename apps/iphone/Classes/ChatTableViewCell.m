//
//  ChatTableViewCell.m
//  IRCLogViewer
//
//  Created by Yuni Kunho Kim on 12/28/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import "ChatTableViewCell.h"


@implementation ChatTableViewCell
@synthesize nick, datetime, talk;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code.
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    
    [super setSelected:selected animated:animated];
    
    // Configure the view for the selected state.
}


- (void)dealloc {
	[nick release];
	[datetime release];
	[talk release];
    [super dealloc];
}


@end
