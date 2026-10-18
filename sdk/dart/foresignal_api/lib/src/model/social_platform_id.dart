//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// Social platform adapter (a 004 source with descriptor, budget, health and credentials). Only `bluesky-jetstream` is on by default (135)
enum SocialPlatformId {
      @JsonValue(r'bluesky-jetstream')
      blueskyJetstream(r'bluesky-jetstream'),
      @JsonValue(r'bluesky-search')
      blueskySearch(r'bluesky-search'),
      @JsonValue(r'mastodon')
      mastodon(r'mastodon'),
      @JsonValue(r'reddit')
      reddit(r'reddit'),
      @JsonValue(r'youtube')
      youtube(r'youtube'),
      @JsonValue(r'x')
      x(r'x'),
      @JsonValue(r'threads')
      threads(r'threads'),
      @JsonValue(r'instagram-hashtags')
      instagramHashtags(r'instagram-hashtags');

  const SocialPlatformId(this.value);

  final String value;

  @override
  String toString() => value;
}
