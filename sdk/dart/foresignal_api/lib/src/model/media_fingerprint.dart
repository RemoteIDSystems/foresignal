//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/media_fingerprint_uses_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_fingerprint.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaFingerprint {
  /// Returns a new [MediaFingerprint] instance.
  MediaFingerprint({

    required  this.id,

    required  this.pdq,

    required  this.quality,

    required  this.matchable,

    required  this.firstSeenAt,

    required  this.firstPostRef,

    required  this.useCount,

    required  this.platforms,

    required  this.narrativeIds,

     this.mediaId,

     this.reuseFindingIds,

     this.uses,

     this.narratives,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// 256-bit PDQ hash, hex
  @JsonKey(
    
    name: r'pdq',
    required: true,
    includeIfNull: false,
  )


  final String pdq;



      /// PDQ quality; below 50 kept but not matched
          // minimum: 0
          // maximum: 100
  @JsonKey(
    
    name: r'quality',
    required: true,
    includeIfNull: false,
  )


  final int quality;



  @JsonKey(
    
    name: r'matchable',
    required: true,
    includeIfNull: false,
  )


  final bool matchable;



  @JsonKey(
    
    name: r'firstSeenAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstSeenAt;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'firstPostRef',
    required: true,
    includeIfNull: false,
  )


  final String firstPostRef;



          // minimum: 0
  @JsonKey(
    
    name: r'useCount',
    required: true,
    includeIfNull: false,
  )


  final int useCount;



  @JsonKey(
    
    name: r'platforms',
    required: true,
    includeIfNull: false,
  )


  final List<String> platforms;



  @JsonKey(
    
    name: r'narrativeIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> narrativeIds;



      /// 043 stored content when `social.keepMedia` is on
  @JsonKey(
    
    name: r'mediaId',
    required: false,
    includeIfNull: false,
  )


  final String? mediaId;



      /// `media-reuse` findings
  @JsonKey(
    
    name: r'reuseFindingIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? reuseFindingIds;



  @JsonKey(
    
    name: r'uses',
    required: false,
    includeIfNull: false,
  )


  final List<MediaFingerprintUsesInner>? uses;



      /// Detail view
  @JsonKey(
    
    name: r'narratives',
    required: false,
    includeIfNull: false,
  )


  final List<ObjectEnvelope>? narratives;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaFingerprint &&
      other.id == id &&
      other.pdq == pdq &&
      other.quality == quality &&
      other.matchable == matchable &&
      other.firstSeenAt == firstSeenAt &&
      other.firstPostRef == firstPostRef &&
      other.useCount == useCount &&
      other.platforms == platforms &&
      other.narrativeIds == narrativeIds &&
      other.mediaId == mediaId &&
      other.reuseFindingIds == reuseFindingIds &&
      other.uses == uses &&
      other.narratives == narratives;

    @override
    int get hashCode =>
        id.hashCode +
        pdq.hashCode +
        quality.hashCode +
        matchable.hashCode +
        firstSeenAt.hashCode +
        firstPostRef.hashCode +
        useCount.hashCode +
        platforms.hashCode +
        narrativeIds.hashCode +
        (mediaId == null ? 0 : mediaId.hashCode) +
        reuseFindingIds.hashCode +
        uses.hashCode +
        narratives.hashCode;

  factory MediaFingerprint.fromJson(Map<String, dynamic> json) => _$MediaFingerprintFromJson(json);

  Map<String, dynamic> toJson() => _$MediaFingerprintToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

