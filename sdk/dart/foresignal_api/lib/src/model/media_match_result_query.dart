//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_match_result_query.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaMatchResultQuery {
  /// Returns a new [MediaMatchResultQuery] instance.
  MediaMatchResultQuery({

    required  this.pdq,

     this.quality,

    required  this.source_,
  });

      /// 256-bit PDQ hash, hex
  @JsonKey(
    
    name: r'pdq',
    required: true,
    includeIfNull: false,
  )


  final String pdq;



          // minimum: 0
          // maximum: 100
  @JsonKey(
    
    name: r'quality',
    required: false,
    includeIfNull: false,
  )


  final int? quality;



  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final MediaMatchResultQuerySource_Enum source_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaMatchResultQuery &&
      other.pdq == pdq &&
      other.quality == quality &&
      other.source_ == source_;

    @override
    int get hashCode =>
        pdq.hashCode +
        (quality == null ? 0 : quality.hashCode) +
        source_.hashCode;

  factory MediaMatchResultQuery.fromJson(Map<String, dynamic> json) => _$MediaMatchResultQueryFromJson(json);

  Map<String, dynamic> toJson() => _$MediaMatchResultQueryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MediaMatchResultQuerySource_Enum {
@JsonValue(r'upload')
upload(r'upload'),
@JsonValue(r'pdq')
pdq(r'pdq');

const MediaMatchResultQuerySource_Enum(this.value);

final String value;

@override
String toString() => value;
}


