//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/outlook_window.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'outlook_published_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OutlookPublishedEvent {
  /// Returns a new [OutlookPublishedEvent] instance.
  OutlookPublishedEvent({

    required  this.region,

    required  this.family,

    required  this.publishedAt,

     this.windows,

     this.recordRef,
  });

  @JsonKey(
    
    name: r'region',
    required: true,
    includeIfNull: false,
  )


  final String region;



  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;



  @JsonKey(
    
    name: r'publishedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime publishedAt;



  @JsonKey(
    
    name: r'windows',
    required: false,
    includeIfNull: false,
  )


  final List<OutlookWindow>? windows;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'recordRef',
    required: false,
    includeIfNull: false,
  )


  final String? recordRef;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OutlookPublishedEvent &&
      other.region == region &&
      other.family == family &&
      other.publishedAt == publishedAt &&
      other.windows == windows &&
      other.recordRef == recordRef;

    @override
    int get hashCode =>
        region.hashCode +
        family.hashCode +
        publishedAt.hashCode +
        windows.hashCode +
        (recordRef == null ? 0 : recordRef.hashCode);

  factory OutlookPublishedEvent.fromJson(Map<String, dynamic> json) => _$OutlookPublishedEventFromJson(json);

  Map<String, dynamic> toJson() => _$OutlookPublishedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

