// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'external_source_pull.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ExternalSourcePullCWProxy {
  ExternalSourcePull cursor(String? cursor);

  ExternalSourcePull lastPollAt(DateTime? lastPollAt);

  ExternalSourcePull lastError(String? lastError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSourcePull(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSourcePull(...).copyWith(id: 12, name: "My name")
  /// ```
  ExternalSourcePull call({
    String? cursor,
    DateTime? lastPollAt,
    String? lastError,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfExternalSourcePull.copyWith(...)` or call `instanceOfExternalSourcePull.copyWith.fieldName(value)` for a single field.
class _$ExternalSourcePullCWProxyImpl implements _$ExternalSourcePullCWProxy {
  const _$ExternalSourcePullCWProxyImpl(this._value);

  final ExternalSourcePull _value;

  @override
  ExternalSourcePull cursor(String? cursor) => call(cursor: cursor);

  @override
  ExternalSourcePull lastPollAt(DateTime? lastPollAt) =>
      call(lastPollAt: lastPollAt);

  @override
  ExternalSourcePull lastError(String? lastError) => call(lastError: lastError);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ExternalSourcePull(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ExternalSourcePull(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ExternalSourcePull call({
    Object? cursor = const $CopyWithPlaceholder(),
    Object? lastPollAt = const $CopyWithPlaceholder(),
    Object? lastError = const $CopyWithPlaceholder(),
  }) {
    return ExternalSourcePull(
      cursor: cursor == const $CopyWithPlaceholder()
          ? _value.cursor
          // ignore: cast_nullable_to_non_nullable
          : cursor as String?,
      lastPollAt: lastPollAt == const $CopyWithPlaceholder()
          ? _value.lastPollAt
          // ignore: cast_nullable_to_non_nullable
          : lastPollAt as DateTime?,
      lastError: lastError == const $CopyWithPlaceholder()
          ? _value.lastError
          // ignore: cast_nullable_to_non_nullable
          : lastError as String?,
    );
  }
}

extension $ExternalSourcePullCopyWith on ExternalSourcePull {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfExternalSourcePull.copyWith(...)` or `instanceOfExternalSourcePull.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ExternalSourcePullCWProxy get copyWith =>
      _$ExternalSourcePullCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ExternalSourcePull _$ExternalSourcePullFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ExternalSourcePull', json, ($checkedConvert) {
      final val = ExternalSourcePull(
        cursor: $checkedConvert('cursor', (v) => v as String?),
        lastPollAt: $checkedConvert(
          'lastPollAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        lastError: $checkedConvert('lastError', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ExternalSourcePullToJson(ExternalSourcePull instance) =>
    <String, dynamic>{
      'cursor': ?instance.cursor,
      'lastPollAt': ?instance.lastPollAt?.toIso8601String(),
      'lastError': ?instance.lastError,
    };
