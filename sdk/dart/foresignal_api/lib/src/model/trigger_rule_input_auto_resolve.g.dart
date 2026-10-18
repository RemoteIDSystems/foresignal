// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trigger_rule_input_auto_resolve.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TriggerRuleInputAutoResolveCWProxy {
  TriggerRuleInputAutoResolve onInverseEdge(bool? onInverseEdge);

  TriggerRuleInputAutoResolve onTombstone(bool? onTombstone);

  TriggerRuleInputAutoResolve onExpiry(bool? onExpiry);

  TriggerRuleInputAutoResolve staleAfter(String? staleAfter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRuleInputAutoResolve(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRuleInputAutoResolve(...).copyWith(id: 12, name: "My name")
  /// ```
  TriggerRuleInputAutoResolve call({
    bool? onInverseEdge,
    bool? onTombstone,
    bool? onExpiry,
    String? staleAfter,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTriggerRuleInputAutoResolve.copyWith(...)` or call `instanceOfTriggerRuleInputAutoResolve.copyWith.fieldName(value)` for a single field.
class _$TriggerRuleInputAutoResolveCWProxyImpl
    implements _$TriggerRuleInputAutoResolveCWProxy {
  const _$TriggerRuleInputAutoResolveCWProxyImpl(this._value);

  final TriggerRuleInputAutoResolve _value;

  @override
  TriggerRuleInputAutoResolve onInverseEdge(bool? onInverseEdge) =>
      call(onInverseEdge: onInverseEdge);

  @override
  TriggerRuleInputAutoResolve onTombstone(bool? onTombstone) =>
      call(onTombstone: onTombstone);

  @override
  TriggerRuleInputAutoResolve onExpiry(bool? onExpiry) =>
      call(onExpiry: onExpiry);

  @override
  TriggerRuleInputAutoResolve staleAfter(String? staleAfter) =>
      call(staleAfter: staleAfter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TriggerRuleInputAutoResolve(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TriggerRuleInputAutoResolve(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TriggerRuleInputAutoResolve call({
    Object? onInverseEdge = const $CopyWithPlaceholder(),
    Object? onTombstone = const $CopyWithPlaceholder(),
    Object? onExpiry = const $CopyWithPlaceholder(),
    Object? staleAfter = const $CopyWithPlaceholder(),
  }) {
    return TriggerRuleInputAutoResolve(
      onInverseEdge: onInverseEdge == const $CopyWithPlaceholder()
          ? _value.onInverseEdge
          // ignore: cast_nullable_to_non_nullable
          : onInverseEdge as bool?,
      onTombstone: onTombstone == const $CopyWithPlaceholder()
          ? _value.onTombstone
          // ignore: cast_nullable_to_non_nullable
          : onTombstone as bool?,
      onExpiry: onExpiry == const $CopyWithPlaceholder()
          ? _value.onExpiry
          // ignore: cast_nullable_to_non_nullable
          : onExpiry as bool?,
      staleAfter: staleAfter == const $CopyWithPlaceholder()
          ? _value.staleAfter
          // ignore: cast_nullable_to_non_nullable
          : staleAfter as String?,
    );
  }
}

extension $TriggerRuleInputAutoResolveCopyWith on TriggerRuleInputAutoResolve {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTriggerRuleInputAutoResolve.copyWith(...)` or `instanceOfTriggerRuleInputAutoResolve.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TriggerRuleInputAutoResolveCWProxy get copyWith =>
      _$TriggerRuleInputAutoResolveCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TriggerRuleInputAutoResolve _$TriggerRuleInputAutoResolveFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TriggerRuleInputAutoResolve', json, ($checkedConvert) {
  final val = TriggerRuleInputAutoResolve(
    onInverseEdge: $checkedConvert('onInverseEdge', (v) => v as bool? ?? true),
    onTombstone: $checkedConvert('onTombstone', (v) => v as bool? ?? true),
    onExpiry: $checkedConvert('onExpiry', (v) => v as bool? ?? true),
    staleAfter: $checkedConvert('staleAfter', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$TriggerRuleInputAutoResolveToJson(
  TriggerRuleInputAutoResolve instance,
) => <String, dynamic>{
  'onInverseEdge': ?instance.onInverseEdge,
  'onTombstone': ?instance.onTombstone,
  'onExpiry': ?instance.onExpiry,
  'staleAfter': ?instance.staleAfter,
};
