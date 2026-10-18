//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/media_match_result_query.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/media_match.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_match_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaMatchResult {
  /// Returns a new [MediaMatchResult] instance.
  MediaMatchResult({

    required  this.query,

    required  this.maxDistance,

    required  this.matches,

     this.referenceWindowStart,

    required  this.caveats,
  });

  @JsonKey(
    
    name: r'query',
    required: true,
    includeIfNull: false,
  )


  final MediaMatchResultQuery query;



  @JsonKey(
    
    name: r'maxDistance',
    required: true,
    includeIfNull: false,
  )


  final int maxDistance;



  @JsonKey(
    
    name: r'matches',
    required: true,
    includeIfNull: false,
  )


  final List<MediaMatch> matches;



      /// Start of the fingerprint store
  @JsonKey(
    
    name: r'referenceWindowStart',
    required: false,
    includeIfNull: false,
  )


  final DateTime? referenceWindowStart;



  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<Caveat> caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaMatchResult &&
      other.query == query &&
      other.maxDistance == maxDistance &&
      other.matches == matches &&
      other.referenceWindowStart == referenceWindowStart &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        query.hashCode +
        maxDistance.hashCode +
        matches.hashCode +
        (referenceWindowStart == null ? 0 : referenceWindowStart.hashCode) +
        caveats.hashCode;

  factory MediaMatchResult.fromJson(Map<String, dynamic> json) => _$MediaMatchResultFromJson(json);

  Map<String, dynamic> toJson() => _$MediaMatchResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

