create schema dbo;

create table dbo.Users(
    id int generated always as identity primary key,
    username varchar(64) unique not null,
    secret  varchar(256) not null,
    createdAt bigint not null
);

create table dbo.UserInvites(
    token varchar(256) primary key,
    createdBy int references dbo.Users(id),
    acceptedBy int references dbo.Users(id) null,
    timestamp bigint not null
);

create table dbo.Sessions(
    token varchar(256) primary key,
    userId int references dbo.Users(id),
    createdAt bigint not null,
    lastUsed bigint not null
);

create table dbo.Channels(
    id int generated always as identity primary key,
    name varchar(64) unique not null,
    type varchar(32) not null,
    lastMessage int null,
    ownerId int references dbo.Users(id),
    createdAt bigint not null
);


create table dbo.ChannelInvites(
    inviteId int generated always as identity primary key,
    senderId int references dbo.Users(id),
    recipientId int references dbo.Users(id),
    channelId int references dbo.Channels(id),
    permissions int not null,
    message varchar(256) null,
    status varchar(32) not null,
    createdAt bigint not null,
    resolvedAt bigint null
);

create table dbo.ChannelUsers(
    userId int references dbo.Users(id),
    channelId int references dbo.Channels(id),
    permissions int not null,
    inviteId int references dbo.ChannelInvites(inviteId) null,
    lastRead int null,
    joinedAt bigint not null
);

create table dbo.Messages(
    id int generated always as identity primary key,
    userId int references dbo.Users(id),
    channelId int references dbo.Channels(id),
    type varchar(32) not null,
    content text null,
    createdAt bigint not null
);
