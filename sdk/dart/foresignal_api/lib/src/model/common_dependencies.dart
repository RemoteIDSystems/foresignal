//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/common_dependencies_dependencies_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'common_dependencies.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CommonDependencies {
  /// Returns a new [CommonDependencies] instance.
  CommonDependencies({

    required  this.refs,

     this.asOf,

     this.generatedAt,

    required  this.dependencies,
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



  @JsonKey(
    
    name: r'generatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? generatedAt;



  @JsonKey(
    
    name: r'dependencies',
    required: true,
    includeIfNull: false,
  )


  final List<CommonDependenciesDependenciesInner> dependencies;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CommonDependencies &&
      other.refs == refs &&
      other.asOf == asOf &&
      other.generatedAt == generatedAt &&
      other.dependencies == dependencies;

    @override
    int get hashCode =>
        refs.hashCode +
        (asOf == null ? 0 : asOf.hashCode) +
        generatedAt.hashCode +
        dependencies.hashCode;

  factory CommonDependencies.fromJson(Map<String, dynamic> json) => _$CommonDependenciesFromJson(json);

  Map<String, dynamic> toJson() => _$CommonDependenciesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

