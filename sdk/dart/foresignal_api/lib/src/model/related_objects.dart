//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/related_object.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'related_objects.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RelatedObjects {
  /// Returns a new [RelatedObjects] instance.
  RelatedObjects({

    required  this.ref,

     this.asOf,

    required  this.items,

     this.truncated,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<RelatedObject> items;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RelatedObjects &&
      other.ref == ref &&
      other.asOf == asOf &&
      other.items == items &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        ref.hashCode +
        (asOf == null ? 0 : asOf.hashCode) +
        items.hashCode +
        truncated.hashCode;

  factory RelatedObjects.fromJson(Map<String, dynamic> json) => _$RelatedObjectsFromJson(json);

  Map<String, dynamic> toJson() => _$RelatedObjectsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

