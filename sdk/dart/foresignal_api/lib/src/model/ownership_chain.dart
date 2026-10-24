//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ownership_chain_node.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ownership_chain.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OwnershipChain {
  /// Returns a new [OwnershipChain] instance.
  OwnershipChain({

    required  this.refs,

    required  this.direction,

     this.asOf,

     this.generatedAt,

    required  this.chains,

    required  this.shared,
  });

  @JsonKey(
    
    name: r'refs',
    required: true,
    includeIfNull: false,
  )


  final List<String> refs;



  @JsonKey(
    
    name: r'direction',
    required: true,
    includeIfNull: false,
  )


  final OwnershipChainDirectionEnum direction;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



  @JsonKey(
    
    name: r'generatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? generatedAt;



      /// Every entity reached from a seed through ownership / affiliation edges, strongest and shortest path first
  @JsonKey(
    
    name: r'chains',
    required: true,
    includeIfNull: false,
  )


  final List<OwnershipChainNode> chains;



      /// The subset of `chains` reached from two or more seeds — a shared owner, officer, address or affiliate
  @JsonKey(
    
    name: r'shared',
    required: true,
    includeIfNull: false,
  )


  final List<OwnershipChainNode> shared;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OwnershipChain &&
      other.refs == refs &&
      other.direction == direction &&
      other.asOf == asOf &&
      other.generatedAt == generatedAt &&
      other.chains == chains &&
      other.shared == shared;

    @override
    int get hashCode =>
        refs.hashCode +
        direction.hashCode +
        (asOf == null ? 0 : asOf.hashCode) +
        generatedAt.hashCode +
        chains.hashCode +
        shared.hashCode;

  factory OwnershipChain.fromJson(Map<String, dynamic> json) => _$OwnershipChainFromJson(json);

  Map<String, dynamic> toJson() => _$OwnershipChainToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OwnershipChainDirectionEnum {
@JsonValue(r'in')
in_(r'in'),
@JsonValue(r'out')
out_(r'out'),
@JsonValue(r'both')
both(r'both');

const OwnershipChainDirectionEnum(this.value);

final String value;

@override
String toString() => value;
}


