//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pack_spec_vector.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PackSpecVector {
  /// Returns a new [PackSpecVector] instance.
  PackSpecVector({

     this.sources,

     this.kinds,

     this.asOf,
  });

  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PackSpecVector &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.asOf == asOf;

    @override
    int get hashCode =>
        sources.hashCode +
        kinds.hashCode +
        (asOf == null ? 0 : asOf.hashCode);

  factory PackSpecVector.fromJson(Map<String, dynamic> json) => _$PackSpecVectorFromJson(json);

  Map<String, dynamic> toJson() => _$PackSpecVectorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

