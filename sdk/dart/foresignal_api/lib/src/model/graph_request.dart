//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'graph_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GraphRequest {
  /// Returns a new [GraphRequest] instance.
  GraphRequest({

    required  this.refs,

     this.depth = 1,

     this.types,

     this.asOf,

     this.minConfidence,

     this.limit = 500,
  });

  @JsonKey(
    
    name: r'refs',
    required: true,
    includeIfNull: false,
  )


  final List<String> refs;



          // minimum: 1
          // maximum: 2
  @JsonKey(
    defaultValue: 1,
    name: r'depth',
    required: false,
    includeIfNull: false,
  )


  final int? depth;



  @JsonKey(
    
    name: r'types',
    required: false,
    includeIfNull: false,
  )


  final List<GraphRequestTypesEnum>? types;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'minConfidence',
    required: false,
    includeIfNull: false,
  )


  final num? minConfidence;



          // minimum: 1
          // maximum: 2000
  @JsonKey(
    defaultValue: 500,
    name: r'limit',
    required: false,
    includeIfNull: false,
  )


  final int? limit;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GraphRequest &&
      other.refs == refs &&
      other.depth == depth &&
      other.types == types &&
      other.asOf == asOf &&
      other.minConfidence == minConfidence &&
      other.limit == limit;

    @override
    int get hashCode =>
        refs.hashCode +
        depth.hashCode +
        types.hashCode +
        (asOf == null ? 0 : asOf.hashCode) +
        minConfidence.hashCode +
        limit.hashCode;

  factory GraphRequest.fromJson(Map<String, dynamic> json) => _$GraphRequestFromJson(json);

  Map<String, dynamic> toJson() => _$GraphRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Client-Contract §4 relation type
enum GraphRequestTypesEnum {
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
relatedTo(r'related-to'),
@JsonValue(r'owns')
owns(r'owns'),
@JsonValue(r'controls')
controls(r'controls'),
@JsonValue(r'officer-of')
officerOf(r'officer-of'),
@JsonValue(r'beneficial-owner')
beneficialOwner(r'beneficial-owner'),
@JsonValue(r'affiliated-with')
affiliatedWith(r'affiliated-with');

const GraphRequestTypesEnum(this.value);

final String value;

@override
String toString() => value;
}


