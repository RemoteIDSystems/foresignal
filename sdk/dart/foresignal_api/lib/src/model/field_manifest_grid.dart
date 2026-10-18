//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_manifest_grid.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldManifestGrid {
  /// Returns a new [FieldManifestGrid] instance.
  FieldManifestGrid({

    required  this.nx,

    required  this.ny,

    required  this.lo1,

    required  this.la1,

    required  this.dx,

    required  this.dy,
  });

  @JsonKey(
    
    name: r'nx',
    required: true,
    includeIfNull: false,
  )


  final int nx;



  @JsonKey(
    
    name: r'ny',
    required: true,
    includeIfNull: false,
  )


  final int ny;



  @JsonKey(
    
    name: r'lo1',
    required: true,
    includeIfNull: false,
  )


  final num lo1;



  @JsonKey(
    
    name: r'la1',
    required: true,
    includeIfNull: false,
  )


  final num la1;



  @JsonKey(
    
    name: r'dx',
    required: true,
    includeIfNull: false,
  )


  final num dx;



  @JsonKey(
    
    name: r'dy',
    required: true,
    includeIfNull: false,
  )


  final num dy;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldManifestGrid &&
      other.nx == nx &&
      other.ny == ny &&
      other.lo1 == lo1 &&
      other.la1 == la1 &&
      other.dx == dx &&
      other.dy == dy;

    @override
    int get hashCode =>
        nx.hashCode +
        ny.hashCode +
        lo1.hashCode +
        la1.hashCode +
        dx.hashCode +
        dy.hashCode;

  factory FieldManifestGrid.fromJson(Map<String, dynamic> json) => _$FieldManifestGridFromJson(json);

  Map<String, dynamic> toJson() => _$FieldManifestGridToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

