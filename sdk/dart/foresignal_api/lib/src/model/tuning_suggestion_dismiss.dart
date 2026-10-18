//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tuning_suggestion_dismiss.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TuningSuggestionDismiss {
  /// Returns a new [TuningSuggestionDismiss] instance.
  TuningSuggestionDismiss({

     this.reason,
  });

  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TuningSuggestionDismiss &&
      other.reason == reason;

    @override
    int get hashCode =>
        (reason == null ? 0 : reason.hashCode);

  factory TuningSuggestionDismiss.fromJson(Map<String, dynamic> json) => _$TuningSuggestionDismissFromJson(json);

  Map<String, dynamic> toJson() => _$TuningSuggestionDismissToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

