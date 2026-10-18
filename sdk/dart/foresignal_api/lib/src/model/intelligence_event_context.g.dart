// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intelligence_event_context.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelligenceEventContextCWProxy {
  IntelligenceEventContext weather(Map<String, Object>? weather);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelligenceEventContext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelligenceEventContext(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelligenceEventContext call({Map<String, Object>? weather});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelligenceEventContext.copyWith(...)` or call `instanceOfIntelligenceEventContext.copyWith.fieldName(value)` for a single field.
class _$IntelligenceEventContextCWProxyImpl
    implements _$IntelligenceEventContextCWProxy {
  const _$IntelligenceEventContextCWProxyImpl(this._value);

  final IntelligenceEventContext _value;

  @override
  IntelligenceEventContext weather(Map<String, Object>? weather) =>
      call(weather: weather);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelligenceEventContext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelligenceEventContext(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelligenceEventContext call({
    Object? weather = const $CopyWithPlaceholder(),
  }) {
    return IntelligenceEventContext(
      weather: weather == const $CopyWithPlaceholder()
          ? _value.weather
          // ignore: cast_nullable_to_non_nullable
          : weather as Map<String, Object>?,
    );
  }
}

extension $IntelligenceEventContextCopyWith on IntelligenceEventContext {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelligenceEventContext.copyWith(...)` or `instanceOfIntelligenceEventContext.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelligenceEventContextCWProxy get copyWith =>
      _$IntelligenceEventContextCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelligenceEventContext _$IntelligenceEventContextFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelligenceEventContext', json, ($checkedConvert) {
  final val = IntelligenceEventContext(
    weather: $checkedConvert(
      'weather',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$IntelligenceEventContextToJson(
  IntelligenceEventContext instance,
) => <String, dynamic>{'weather': ?instance.weather};
