// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_board_indicators_observed_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelBoardIndicatorsObservedInnerCWProxy {
  IntelBoardIndicatorsObservedInner indicatorId(String indicatorId);

  IntelBoardIndicatorsObservedInner setId(String setId);

  IntelBoardIndicatorsObservedInner description(String? description);

  IntelBoardIndicatorsObservedInner at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardIndicatorsObservedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardIndicatorsObservedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelBoardIndicatorsObservedInner call({
    String indicatorId,
    String setId,
    String? description,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelBoardIndicatorsObservedInner.copyWith(...)` or call `instanceOfIntelBoardIndicatorsObservedInner.copyWith.fieldName(value)` for a single field.
class _$IntelBoardIndicatorsObservedInnerCWProxyImpl
    implements _$IntelBoardIndicatorsObservedInnerCWProxy {
  const _$IntelBoardIndicatorsObservedInnerCWProxyImpl(this._value);

  final IntelBoardIndicatorsObservedInner _value;

  @override
  IntelBoardIndicatorsObservedInner indicatorId(String indicatorId) =>
      call(indicatorId: indicatorId);

  @override
  IntelBoardIndicatorsObservedInner setId(String setId) => call(setId: setId);

  @override
  IntelBoardIndicatorsObservedInner description(String? description) =>
      call(description: description);

  @override
  IntelBoardIndicatorsObservedInner at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardIndicatorsObservedInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardIndicatorsObservedInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelBoardIndicatorsObservedInner call({
    Object? indicatorId = const $CopyWithPlaceholder(),
    Object? setId = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return IntelBoardIndicatorsObservedInner(
      indicatorId:
          indicatorId == const $CopyWithPlaceholder() || indicatorId == null
          ? _value.indicatorId
          // ignore: cast_nullable_to_non_nullable
          : indicatorId as String,
      setId: setId == const $CopyWithPlaceholder() || setId == null
          ? _value.setId
          // ignore: cast_nullable_to_non_nullable
          : setId as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $IntelBoardIndicatorsObservedInnerCopyWith
    on IntelBoardIndicatorsObservedInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelBoardIndicatorsObservedInner.copyWith(...)` or `instanceOfIntelBoardIndicatorsObservedInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelBoardIndicatorsObservedInnerCWProxy get copyWith =>
      _$IntelBoardIndicatorsObservedInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelBoardIndicatorsObservedInner _$IntelBoardIndicatorsObservedInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelBoardIndicatorsObservedInner', json, (
  $checkedConvert,
) {
  $checkKeys(json, requiredKeys: const ['indicatorId', 'setId', 'at']);
  final val = IntelBoardIndicatorsObservedInner(
    indicatorId: $checkedConvert('indicatorId', (v) => v as String),
    setId: $checkedConvert('setId', (v) => v as String),
    description: $checkedConvert('description', (v) => v as String?),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$IntelBoardIndicatorsObservedInnerToJson(
  IntelBoardIndicatorsObservedInner instance,
) => <String, dynamic>{
  'indicatorId': instance.indicatorId,
  'setId': instance.setId,
  'description': ?instance.description,
  'at': instance.at.toIso8601String(),
};
