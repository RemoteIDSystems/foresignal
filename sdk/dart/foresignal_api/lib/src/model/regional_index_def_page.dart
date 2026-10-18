//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/regional_index_def.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_index_def_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalIndexDefPage {
  /// Returns a new [RegionalIndexDefPage] instance.
  RegionalIndexDefPage({

    required  this.items,

     this.nextCursor,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<RegionalIndexDef> items;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalIndexDefPage &&
      other.items == items &&
      other.nextCursor == nextCursor;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode);

  factory RegionalIndexDefPage.fromJson(Map<String, dynamic> json) => _$RegionalIndexDefPageFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalIndexDefPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

