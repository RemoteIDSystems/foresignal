//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'object_resolve_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObjectResolveResult {
  /// Returns a new [ObjectResolveResult] instance.
  ObjectResolveResult({

    required  this.items,

    required  this.missing,

     this.asOf,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<ObjectEnvelope> items;



  @JsonKey(
    
    name: r'missing',
    required: true,
    includeIfNull: false,
  )


  final List<String> missing;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObjectResolveResult &&
      other.items == items &&
      other.missing == missing &&
      other.asOf == asOf;

    @override
    int get hashCode =>
        items.hashCode +
        missing.hashCode +
        (asOf == null ? 0 : asOf.hashCode);

  factory ObjectResolveResult.fromJson(Map<String, dynamic> json) => _$ObjectResolveResultFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectResolveResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

