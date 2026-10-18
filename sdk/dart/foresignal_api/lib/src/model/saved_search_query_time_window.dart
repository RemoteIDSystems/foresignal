//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'saved_search_query_time_window.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SavedSearchQueryTimeWindow {
  /// Returns a new [SavedSearchQueryTimeWindow] instance.
  SavedSearchQueryTimeWindow({

     this.start,

     this.end,

     this.relative,
  });

  @JsonKey(
    
    name: r'start',
    required: false,
    includeIfNull: false,
  )


  final DateTime? start;



  @JsonKey(
    
    name: r'end',
    required: false,
    includeIfNull: false,
  )


  final DateTime? end;



      /// ISO-8601 duration back from now, e.g. `P7D`
  @JsonKey(
    
    name: r'relative',
    required: false,
    includeIfNull: false,
  )


  final String? relative;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SavedSearchQueryTimeWindow &&
      other.start == start &&
      other.end == end &&
      other.relative == relative;

    @override
    int get hashCode =>
        (start == null ? 0 : start.hashCode) +
        (end == null ? 0 : end.hashCode) +
        (relative == null ? 0 : relative.hashCode);

  factory SavedSearchQueryTimeWindow.fromJson(Map<String, dynamic> json) => _$SavedSearchQueryTimeWindowFromJson(json);

  Map<String, dynamic> toJson() => _$SavedSearchQueryTimeWindowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

