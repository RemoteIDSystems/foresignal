//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'relation_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RelationInput {
  /// Returns a new [RelationInput] instance.
  RelationInput({

    required  this.fromRef,

    required  this.toRef,

    required  this.type,

     this.validFrom,

     this.validTo,

     this.confidence = 1,

     this.label,

     this.private = false,
  });

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



      /// Client-Contract §4 relation type
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final RelationInputTypeEnum type;



  @JsonKey(
    
    name: r'validFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validFrom;



  @JsonKey(
    
    name: r'validTo',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTo;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    defaultValue: 1,
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final num? confidence;



      /// Relation or dependency label (104 vocabulary)
  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final RelationInputLabelEnum? label;



  @JsonKey(
    defaultValue: false,
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RelationInput &&
      other.fromRef == fromRef &&
      other.toRef == toRef &&
      other.type == type &&
      other.validFrom == validFrom &&
      other.validTo == validTo &&
      other.confidence == confidence &&
      other.label == label &&
      other.private == private;

    @override
    int get hashCode =>
        fromRef.hashCode +
        toRef.hashCode +
        type.hashCode +
        (validFrom == null ? 0 : validFrom.hashCode) +
        (validTo == null ? 0 : validTo.hashCode) +
        confidence.hashCode +
        label.hashCode +
        private.hashCode;

  factory RelationInput.fromJson(Map<String, dynamic> json) => _$RelationInputFromJson(json);

  Map<String, dynamic> toJson() => _$RelationInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Client-Contract §4 relation type
enum RelationInputTypeEnum {
@JsonValue(r'member-of')
memberOf(r'member-of'),
@JsonValue(r'observed-by')
observedBy(r'observed-by'),
@JsonValue(r'same-as')
sameAs(r'same-as'),
@JsonValue(r'located-in')
locatedIn(r'located-in'),
@JsonValue(r'near')
near(r'near'),
@JsonValue(r'depends-on')
dependsOn(r'depends-on'),
@JsonValue(r'affects')
affects(r'affects'),
@JsonValue(r'derived-from')
derivedFrom(r'derived-from'),
@JsonValue(r'evidence-for')
evidenceFor(r'evidence-for'),
@JsonValue(r'evidence-against')
evidenceAgainst(r'evidence-against'),
@JsonValue(r'about')
about(r'about'),
@JsonValue(r'mentions')
mentions(r'mentions'),
@JsonValue(r'precedes')
precedes(r'precedes'),
@JsonValue(r'causes')
causes(r'causes'),
@JsonValue(r'related-to')
relatedTo(r'related-to');

const RelationInputTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Relation or dependency label (104 vocabulary)
enum RelationInputLabelEnum {
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'likely')
likely(r'likely'),
@JsonValue(r'possible')
possible(r'possible'),
@JsonValue(r'speculative')
speculative(r'speculative');

const RelationInputLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


