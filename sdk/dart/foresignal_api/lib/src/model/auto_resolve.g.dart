// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'auto_resolve.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AutoResolveCWProxy {
  AutoResolve onInverseEdge(bool? onInverseEdge);

  AutoResolve onTombstone(bool? onTombstone);

  AutoResolve onExpiry(bool? onExpiry);

  AutoResolve staleAfter(String? staleAfter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AutoResolve(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AutoResolve(...).copyWith(id: 12, name: "My name")
  /// ```
  AutoResolve call({
    bool? onInverseEdge,
    bool? onTombstone,
    bool? onExpiry,
    String? staleAfter,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAutoResolve.copyWith(...)` or call `instanceOfAutoResolve.copyWith.fieldName(value)` for a single field.
class _$AutoResolveCWProxyImpl implements _$AutoResolveCWProxy {
  const _$AutoResolveCWProxyImpl(this._value);

  final AutoResolve _value;

  @override
  AutoResolve onInverseEdge(bool? onInverseEdge) =>
      call(onInverseEdge: onInverseEdge);

  @override
  AutoResolve onTombstone(bool? onTombstone) => call(onTombstone: onTombstone);

  @override
  AutoResolve onExpiry(bool? onExpiry) => call(onExpiry: onExpiry);

  @override
  AutoResolve staleAfter(String? staleAfter) => call(staleAfter: staleAfter);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AutoResolve(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AutoResolve(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AutoResolve call({
    Object? onInverseEdge = const $CopyWithPlaceholder(),
    Object? onTombstone = const $CopyWithPlaceholder(),
    Object? onExpiry = const $CopyWithPlaceholder(),
    Object? staleAfter = const $CopyWithPlaceholder(),
  }) {
    return AutoResolve(
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

extension $AutoResolveCopyWith on AutoResolve {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAutoResolve.copyWith(...)` or `instanceOfAutoResolve.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AutoResolveCWProxy get copyWith => _$AutoResolveCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AutoResolve _$AutoResolveFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AutoResolve', json, ($checkedConvert) {
      final val = AutoResolve(
        onInverseEdge: $checkedConvert(
          'onInverseEdge',
          (v) => v as bool? ?? true,
        ),
        onTombstone: $checkedConvert('onTombstone', (v) => v as bool? ?? true),
        onExpiry: $checkedConvert('onExpiry', (v) => v as bool? ?? true),
        staleAfter: $checkedConvert('staleAfter', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$AutoResolveToJson(AutoResolve instance) =>
    <String, dynamic>{
      'onInverseEdge': ?instance.onInverseEdge,
      'onTombstone': ?instance.onTombstone,
      'onExpiry': ?instance.onExpiry,
      'staleAfter': ?instance.staleAfter,
    };
