//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tuning_suggestion_expected_effect.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TuningSuggestionExpectedEffect {
  /// Returns a new [TuningSuggestionExpectedEffect] instance.
  TuningSuggestionExpectedEffect({

     this.wouldFire,

     this.removedNegatives,

     this.keptPositives,

     this.backtestId,
  });

  @JsonKey(
    
    name: r'wouldFire',
    required: false,
    includeIfNull: false,
  )


  final int? wouldFire;



  @JsonKey(
    
    name: r'removedNegatives',
    required: false,
    includeIfNull: false,
  )


  final int? removedNegatives;



  @JsonKey(
    
    name: r'keptPositives',
    required: false,
    includeIfNull: false,
  )


  final int? keptPositives;



      /// ULID identifier
  @JsonKey(
    
    name: r'backtestId',
    required: false,
    includeIfNull: false,
  )


  final String? backtestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TuningSuggestionExpectedEffect &&
      other.wouldFire == wouldFire &&
      other.removedNegatives == removedNegatives &&
      other.keptPositives == keptPositives &&
      other.backtestId == backtestId;

    @override
    int get hashCode =>
        wouldFire.hashCode +
        removedNegatives.hashCode +
        keptPositives.hashCode +
        (backtestId == null ? 0 : backtestId.hashCode);

  factory TuningSuggestionExpectedEffect.fromJson(Map<String, dynamic> json) => _$TuningSuggestionExpectedEffectFromJson(json);

  Map<String, dynamic> toJson() => _$TuningSuggestionExpectedEffectToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

