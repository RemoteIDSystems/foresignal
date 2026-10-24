//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ownership_chain_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OwnershipChainRequest {
  /// Returns a new [OwnershipChainRequest] instance.
  OwnershipChainRequest({

    required  this.refs,

     this.depth = 6,

     this.direction,

     this.types,

     this.minConfidence = 0,

     this.asOf,
  });

  @JsonKey(
    
    name: r'refs',
    required: true,
    includeIfNull: false,
  )


  final List<String> refs;



      /// Most ownership / affiliation hops to follow
          // minimum: 1
          // maximum: 6
  @JsonKey(
    defaultValue: 6,
    name: r'depth',
    required: false,
    includeIfNull: false,
  )


  final int? depth;



      /// Follow edges out of the seed (what it owns / controls), into it (who owns / controls it), or both
  @JsonKey(
    
    name: r'direction',
    required: false,
    includeIfNull: false,
  )


  final OwnershipChainRequestDirectionEnum? direction;



      /// Ownership / affiliation edge types to walk; empty or absent walks all five
  @JsonKey(
    
    name: r'types',
    required: false,
    includeIfNull: false,
  )


  final List<OwnershipChainRequestTypesEnum>? types;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    defaultValue: 0,
    name: r'minConfidence',
    required: false,
    includeIfNull: false,
  )


  final num? minConfidence;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OwnershipChainRequest &&
      other.refs == refs &&
      other.depth == depth &&
      other.direction == direction &&
      other.types == types &&
      other.minConfidence == minConfidence &&
      other.asOf == asOf;

    @override
    int get hashCode =>
        refs.hashCode +
        depth.hashCode +
        direction.hashCode +
        types.hashCode +
        minConfidence.hashCode +
        (asOf == null ? 0 : asOf.hashCode);

  factory OwnershipChainRequest.fromJson(Map<String, dynamic> json) => _$OwnershipChainRequestFromJson(json);

  Map<String, dynamic> toJson() => _$OwnershipChainRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Follow edges out of the seed (what it owns / controls), into it (who owns / controls it), or both
enum OwnershipChainRequestDirectionEnum {
@JsonValue(r'in')
in_(r'in'),
@JsonValue(r'out')
out_(r'out'),
@JsonValue(r'both')
both(r'both');

const OwnershipChainRequestDirectionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum OwnershipChainRequestTypesEnum {
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

const OwnershipChainRequestTypesEnum(this.value);

final String value;

@override
String toString() => value;
}


