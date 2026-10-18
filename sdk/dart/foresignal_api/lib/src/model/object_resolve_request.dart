//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'object_resolve_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObjectResolveRequest {
  /// Returns a new [ObjectResolveRequest] instance.
  ObjectResolveRequest({

    required  this.refs,

     this.asOf,
  });

  @JsonKey(
    
    name: r'refs',
    required: true,
    includeIfNull: false,
  )


  final List<String> refs;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObjectResolveRequest &&
      other.refs == refs &&
      other.asOf == asOf;

    @override
    int get hashCode =>
        refs.hashCode +
        (asOf == null ? 0 : asOf.hashCode);

  factory ObjectResolveRequest.fromJson(Map<String, dynamic> json) => _$ObjectResolveRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectResolveRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

