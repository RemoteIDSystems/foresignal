//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/exercise_run.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'exercise_run_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExerciseRunPage {
  /// Returns a new [ExerciseRunPage] instance.
  ExerciseRunPage({

    required  this.items,

     this.nextCursor,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<ExerciseRun> items;



      /// Pass as `cursor` for the next page; null on the last page
  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExerciseRunPage &&
      other.items == items &&
      other.nextCursor == nextCursor;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode);

  factory ExerciseRunPage.fromJson(Map<String, dynamic> json) => _$ExerciseRunPageFromJson(json);

  Map<String, dynamic> toJson() => _$ExerciseRunPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

