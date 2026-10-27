//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'egress_health_sources_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EgressHealthSourcesInner {
  /// Returns a new [EgressHealthSourcesInner] instance.
  EgressHealthSourcesInner({

    required  this.sourceId,

    required  this.backoffUntil,

    required  this.consecutiveThrottles,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'backoffUntil',
    required: true,
    includeIfNull: false,
  )


  final DateTime backoffUntil;



  @JsonKey(
    
    name: r'consecutiveThrottles',
    required: true,
    includeIfNull: false,
  )


  final int consecutiveThrottles;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EgressHealthSourcesInner &&
      other.sourceId == sourceId &&
      other.backoffUntil == backoffUntil &&
      other.consecutiveThrottles == consecutiveThrottles;

    @override
    int get hashCode =>
        sourceId.hashCode +
        backoffUntil.hashCode +
        consecutiveThrottles.hashCode;

  factory EgressHealthSourcesInner.fromJson(Map<String, dynamic> json) => _$EgressHealthSourcesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$EgressHealthSourcesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

