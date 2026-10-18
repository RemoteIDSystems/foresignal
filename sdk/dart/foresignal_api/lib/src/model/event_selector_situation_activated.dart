//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_theme.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_selector_situation_activated.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventSelectorSituationActivated {
  /// Returns a new [EventSelectorSituationActivated] instance.
  EventSelectorSituationActivated({

     this.theme,
  });

  @JsonKey(
    
    name: r'theme',
    required: false,
    includeIfNull: false,
  )


  final SituationTheme? theme;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventSelectorSituationActivated &&
      other.theme == theme;

    @override
    int get hashCode =>
        (theme == null ? 0 : theme.hashCode);

  factory EventSelectorSituationActivated.fromJson(Map<String, dynamic> json) => _$EventSelectorSituationActivatedFromJson(json);

  Map<String, dynamic> toJson() => _$EventSelectorSituationActivatedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

