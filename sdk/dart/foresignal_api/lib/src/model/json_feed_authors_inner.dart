//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'json_feed_authors_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class JsonFeedAuthorsInner {
  /// Returns a new [JsonFeedAuthorsInner] instance.
  JsonFeedAuthorsInner({

     this.name,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;





    @override
    bool operator ==(Object other) => identical(this, other) || other is JsonFeedAuthorsInner &&
      other.name == name;

    @override
    int get hashCode =>
        name.hashCode;

  factory JsonFeedAuthorsInner.fromJson(Map<String, dynamic> json) => _$JsonFeedAuthorsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$JsonFeedAuthorsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

