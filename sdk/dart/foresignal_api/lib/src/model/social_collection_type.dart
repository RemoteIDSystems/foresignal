//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// Measured versus sampled (135): `complete` stream for its filter (Jetstream, Mastodon hashtag stream, X counts), `paged` search (complete within the API's window and limits), `sampled` ranked or targeted sample (top-post search, model-mediated search, cues). Only complete and paged paths feed baselines and velocity
enum SocialCollectionType {
      @JsonValue(r'complete')
      complete(r'complete'),
      @JsonValue(r'paged')
      paged(r'paged'),
      @JsonValue(r'sampled')
      sampled(r'sampled');

  const SocialCollectionType(this.value);

  final String value;

  @override
  String toString() => value;
}
