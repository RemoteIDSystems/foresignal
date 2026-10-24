//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/entity_key.dart';
import 'package:foresignal_api/src/model/entity_alias.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'entity.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Entity {
  /// Returns a new [Entity] instance.
  Entity({

    required  this.id,

     this.requestedId,

    required  this.type,

     this.displayName,

     this.attributes,

    required  this.provisional,

    required  this.status,

     this.mergedInto,

     this.keys,

     this.aliases,

     this.openLinkCounts,

    required  this.createdAt,

    required  this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Set when the requested id was merged into this canonical entity
  @JsonKey(
    
    name: r'requestedId',
    required: false,
    includeIfNull: false,
  )


  final String? requestedId;



      /// Expected to grow (`x-extensible-enum`): a client must accept a value not listed here.
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



      /// Precedence: registration > vessel name > satellite name > key
  @JsonKey(
    
    name: r'displayName',
    required: false,
    includeIfNull: false,
  )


  final String? displayName;



      /// Declared attributes copied from records (≤ 16 KB); registry origin wins over observations. Never owner PII.
  @JsonKey(
    
    name: r'attributes',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? attributes;



      /// True when created from weak keys only
  @JsonKey(
    
    name: r'provisional',
    required: true,
    includeIfNull: false,
  )


  final bool provisional;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final EntityStatusEnum status;



      /// ULID identifier
  @JsonKey(
    
    name: r'mergedInto',
    required: false,
    includeIfNull: false,
  )


  final String? mergedInto;



  @JsonKey(
    
    name: r'keys',
    required: false,
    includeIfNull: false,
  )


  final List<EntityKey>? keys;



  @JsonKey(
    
    name: r'aliases',
    required: false,
    includeIfNull: false,
  )


  final List<EntityAlias>? aliases;



      /// Open links per source id (links from private sources are counted only for admin tokens)
  @JsonKey(
    
    name: r'openLinkCounts',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? openLinkCounts;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Entity &&
      other.id == id &&
      other.requestedId == requestedId &&
      other.type == type &&
      other.displayName == displayName &&
      other.attributes == attributes &&
      other.provisional == provisional &&
      other.status == status &&
      other.mergedInto == mergedInto &&
      other.keys == keys &&
      other.aliases == aliases &&
      other.openLinkCounts == openLinkCounts &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        (requestedId == null ? 0 : requestedId.hashCode) +
        type.hashCode +
        (displayName == null ? 0 : displayName.hashCode) +
        attributes.hashCode +
        provisional.hashCode +
        status.hashCode +
        (mergedInto == null ? 0 : mergedInto.hashCode) +
        keys.hashCode +
        aliases.hashCode +
        openLinkCounts.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Entity.fromJson(Map<String, dynamic> json) => _$EntityFromJson(json);

  Map<String, dynamic> toJson() => _$EntityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EntityStatusEnum {
@JsonValue(r'ACTIVE')
ACTIVE(r'ACTIVE'),
@JsonValue(r'MERGED')
MERGED(r'MERGED');

const EntityStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


