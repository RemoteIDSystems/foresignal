// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_selector_situation_activated.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventSelectorSituationActivatedCWProxy {
  EventSelectorSituationActivated theme(SituationTheme? theme);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventSelectorSituationActivated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventSelectorSituationActivated(...).copyWith(id: 12, name: "My name")
  /// ```
  EventSelectorSituationActivated call({SituationTheme? theme});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventSelectorSituationActivated.copyWith(...)` or call `instanceOfEventSelectorSituationActivated.copyWith.fieldName(value)` for a single field.
class _$EventSelectorSituationActivatedCWProxyImpl
    implements _$EventSelectorSituationActivatedCWProxy {
  const _$EventSelectorSituationActivatedCWProxyImpl(this._value);

  final EventSelectorSituationActivated _value;

  @override
  EventSelectorSituationActivated theme(SituationTheme? theme) =>
      call(theme: theme);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventSelectorSituationActivated(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventSelectorSituationActivated(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventSelectorSituationActivated call({
    Object? theme = const $CopyWithPlaceholder(),
  }) {
    return EventSelectorSituationActivated(
      theme: theme == const $CopyWithPlaceholder()
          ? _value.theme
          // ignore: cast_nullable_to_non_nullable
          : theme as SituationTheme?,
    );
  }
}

extension $EventSelectorSituationActivatedCopyWith
    on EventSelectorSituationActivated {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventSelectorSituationActivated.copyWith(...)` or `instanceOfEventSelectorSituationActivated.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventSelectorSituationActivatedCWProxy get copyWith =>
      _$EventSelectorSituationActivatedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventSelectorSituationActivated _$EventSelectorSituationActivatedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventSelectorSituationActivated', json, ($checkedConvert) {
  final val = EventSelectorSituationActivated(
    theme: $checkedConvert(
      'theme',
      (v) => $enumDecodeNullable(_$SituationThemeEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$EventSelectorSituationActivatedToJson(
  EventSelectorSituationActivated instance,
) => <String, dynamic>{'theme': ?_$SituationThemeEnumMap[instance.theme]};

const _$SituationThemeEnumMap = {
  SituationTheme.maritime: 'maritime',
  SituationTheme.aviation: 'aviation',
  SituationTheme.conflict: 'conflict',
  SituationTheme.infrastructure: 'infrastructure',
  SituationTheme.weather: 'weather',
  SituationTheme.cyber: 'cyber',
  SituationTheme.markets: 'markets',
  SituationTheme.health: 'health',
  SituationTheme.space: 'space',
  SituationTheme.mixed: 'mixed',
};
