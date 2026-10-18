//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/media_fingerprint.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_match.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaMatch {
  /// Returns a new [MediaMatch] instance.
  MediaMatch({

    required  this.fingerprint,

    required  this.distance,
  });

  @JsonKey(
    
    name: r'fingerprint',
    required: true,
    includeIfNull: false,
  )


  final MediaFingerprint fingerprint;



      /// Hamming distance
          // minimum: 0
          // maximum: 256
  @JsonKey(
    
    name: r'distance',
    required: true,
    includeIfNull: false,
  )


  final int distance;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaMatch &&
      other.fingerprint == fingerprint &&
      other.distance == distance;

    @override
    int get hashCode =>
        fingerprint.hashCode +
        distance.hashCode;

  factory MediaMatch.fromJson(Map<String, dynamic> json) => _$MediaMatchFromJson(json);

  Map<String, dynamic> toJson() => _$MediaMatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

