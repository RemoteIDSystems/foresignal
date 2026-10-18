//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tuning_suggestion_apply.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TuningSuggestionApply {
  /// Returns a new [TuningSuggestionApply] instance.
  TuningSuggestionApply({

     this.note,
  });

  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TuningSuggestionApply &&
      other.note == note;

    @override
    int get hashCode =>
        (note == null ? 0 : note.hashCode);

  factory TuningSuggestionApply.fromJson(Map<String, dynamic> json) => _$TuningSuggestionApplyFromJson(json);

  Map<String, dynamic> toJson() => _$TuningSuggestionApplyToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

