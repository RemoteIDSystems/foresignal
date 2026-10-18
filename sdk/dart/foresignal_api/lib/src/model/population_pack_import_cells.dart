//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'population_pack_import_cells.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PopulationPackImportCells {
  /// Returns a new [PopulationPackImportCells] instance.
  PopulationPackImportCells({

     this.res5,

     this.res7,
  });

  @JsonKey(
    
    name: r'res5',
    required: false,
    includeIfNull: false,
  )


  final int? res5;



  @JsonKey(
    
    name: r'res7',
    required: false,
    includeIfNull: false,
  )


  final int? res7;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PopulationPackImportCells &&
      other.res5 == res5 &&
      other.res7 == res7;

    @override
    int get hashCode =>
        res5.hashCode +
        res7.hashCode;

  factory PopulationPackImportCells.fromJson(Map<String, dynamic> json) => _$PopulationPackImportCellsFromJson(json);

  Map<String, dynamic> toJson() => _$PopulationPackImportCellsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

