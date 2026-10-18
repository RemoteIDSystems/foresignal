//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'common_dependencies_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CommonDependenciesRequest {
  /// Returns a new [CommonDependenciesRequest] instance.
  CommonDependenciesRequest({

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
    bool operator ==(Object other) => identical(this, other) || other is CommonDependenciesRequest &&
      other.refs == refs &&
      other.asOf == asOf;

    @override
    int get hashCode =>
        refs.hashCode +
        (asOf == null ? 0 : asOf.hashCode);

  factory CommonDependenciesRequest.fromJson(Map<String, dynamic> json) => _$CommonDependenciesRequestFromJson(json);

  Map<String, dynamic> toJson() => _$CommonDependenciesRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

