//
//  ChatTableViewCell.h
//  IRCLogViewer
//
//  Created by Yuni Kunho Kim on 12/28/10.
//  Copyright 2010 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface ChatTableViewCell : UITableViewCell {
	IBOutlet UILabel *nick;
	IBOutlet UILabel *datetime;
	IBOutlet UILabel *talk;
}

@property (nonatomic, retain) UILabel *nick;
@property (nonatomic, retain) UILabel *datetime;
@property (nonatomic, retain) UILabel *talk;

@end
