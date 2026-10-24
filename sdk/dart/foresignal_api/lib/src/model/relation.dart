//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/relation_provenance.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'relation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Relation {
  /// Returns a new [Relation] instance.
  Relation({

    required  this.id,

    required  this.fromRef,

    required  this.toRef,

    required  this.type,

    required  this.validFrom,

     this.validTo,

    required  this.confidence,

    required  this.label,

    required  this.provenance,

    required  this.projected,

     this.private,

    required  this.createdAt,

     this.retiredAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'fromRef',
    required: true,
    includeIfNull: false,
  )


  final String fromRef;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'toRef',
    required: true,
    includeIfNull: false,
  )


  final String toRef;



      /// Client-Contract §4 relation type. Expected to grow (`x-extensible-enum`): a client must accept a value not listed here.
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



  @JsonKey(
    
    name: r'validFrom',
    required: true,
    includeIfNull: false,
  )


  final DateTime validFrom;



  @JsonKey(
    
    name: r'validTo',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTo;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'confidence',
    required: true,
    includeIfNull: false,
  )


  final num confidence;



      /// Relation or dependency label (104 vocabulary)
  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final RelationLabelEnum label;



  @JsonKey(
    
    name: r'provenance',
    required: true,
    includeIfNull: false,
  )


  final RelationProvenance provenance;



      /// Materialised by `RelationProjector` (not editable)
  @JsonKey(
    
    name: r'projected',
    required: true,
    includeIfNull: false,
  )


  final bool projected;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'retiredAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? retiredAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Relation &&
      other.id == id &&
      other.fromRef == fromRef &&
      other.toRef == toRef &&
      other.type == type &&
      other.validFrom == validFrom &&
      other.validTo == validTo &&
      other.confidence == confidence &&
      other.label == label &&
      other.provenance == provenance &&
      other.projected == projected &&
      other.private == private &&
      other.createdAt == createdAt &&
      other.retiredAt == retiredAt;

    @override
    int get hashCode =>
        id.hashCode +
        fromRef.hashCode +
        toRef.hashCode +
        type.hashCode +
        validFrom.hashCode +
        (validTo == null ? 0 : validTo.hashCode) +
        confidence.hashCode +
        label.hashCode +
        provenance.hashCode +
        projected.hashCode +
        private.hashCode +
        createdAt.hashCode +
        (retiredAt == null ? 0 : retiredAt.hashCode);

  factory Relation.fromJson(Map<String, dynamic> json) => _$RelationFromJson(json);

  Map<String, dynamic> toJson() => _$RelationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Relation or dependency label (104 vocabulary)
enum RelationLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const RelationLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


