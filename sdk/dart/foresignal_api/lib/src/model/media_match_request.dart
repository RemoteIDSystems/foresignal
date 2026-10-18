//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_match_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaMatchRequest {
  /// Returns a new [MediaMatchRequest] instance.
  MediaMatchRequest({

    required  this.pdq,

     this.maxDistance = 31,

     this.limit = 20,
  });

      /// 256-bit PDQ hash, hex
  @JsonKey(
    
    name: r'pdq',
    required: true,
    includeIfNull: false,
  )


  final String pdq;



      /// PDQ's recommended threshold is 31
          // minimum: 0
          // maximum: 63
  @JsonKey(
    defaultValue: 31,
    name: r'maxDistance',
    required: false,
    includeIfNull: false,
  )


  final int? maxDistance;



          // minimum: 1
          // maximum: 100
  @JsonKey(
    defaultValue: 20,
    name: r'limit',
    required: false,
    includeIfNull: false,
  )


  final int? limit;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaMatchRequest &&
      other.pdq == pdq &&
      other.maxDistance == maxDistance &&
      other.limit == limit;

    @override
    int get hashCode =>
        pdq.hashCode +
        maxDistance.hashCode +
        limit.hashCode;

  factory MediaMatchRequest.fromJson(Map<String, dynamic> json) => _$MediaMatchRequestFromJson(json);

  Map<String, dynamic> toJson() => _$MediaMatchRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

