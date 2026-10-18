//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'json_feed_item_authors_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class JsonFeedItemAuthorsInner {
  /// Returns a new [JsonFeedItemAuthorsInner] instance.
  JsonFeedItemAuthorsInner({

     this.name,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;





    @override
    bool operator ==(Object other) => identical(this, other) || other is JsonFeedItemAuthorsInner &&
      other.name == name;

    @override
    int get hashCode =>
        name.hashCode;

  factory JsonFeedItemAuthorsInner.fromJson(Map<String, dynamic> json) => _$JsonFeedItemAuthorsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$JsonFeedItemAuthorsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

