//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_fingerprint_uses_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaFingerprintUsesInner {
  /// Returns a new [MediaFingerprintUsesInner] instance.
  MediaFingerprintUsesInner({

    required  this.postRef,

    required  this.seenAt,

     this.platform,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'postRef',
    required: true,
    includeIfNull: false,
  )


  final String postRef;



  @JsonKey(
    
    name: r'seenAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime seenAt;



      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: false,
    includeIfNull: false,
  )


  final String? platform;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaFingerprintUsesInner &&
      other.postRef == postRef &&
      other.seenAt == seenAt &&
      other.platform == platform;

    @override
    int get hashCode =>
        postRef.hashCode +
        seenAt.hashCode +
        platform.hashCode;

  factory MediaFingerprintUsesInner.fromJson(Map<String, dynamic> json) => _$MediaFingerprintUsesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$MediaFingerprintUsesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

