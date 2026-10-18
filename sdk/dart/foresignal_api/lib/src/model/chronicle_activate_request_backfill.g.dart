// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chronicle_activate_request_backfill.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChronicleActivateRequestBackfillCWProxy {
  ChronicleActivateRequestBackfill from(DateTime from);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleActivateRequestBackfill(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleActivateRequestBackfill(...).copyWith(id: 12, name: "My name")
  /// ```
  ChronicleActivateRequestBackfill call({DateTime from});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChronicleActivateRequestBackfill.copyWith(...)` or call `instanceOfChronicleActivateRequestBackfill.copyWith.fieldName(value)` for a single field.
class _$ChronicleActivateRequestBackfillCWProxyImpl
    implements _$ChronicleActivateRequestBackfillCWProxy {
  const _$ChronicleActivateRequestBackfillCWProxyImpl(this._value);

  final ChronicleActivateRequestBackfill _value;

  @override
  ChronicleActivateRequestBackfill from(DateTime from) => call(from: from);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChronicleActivateRequestBackfill(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChronicleActivateRequestBackfill(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChronicleActivateRequestBackfill call({
    Object? from = const $CopyWithPlaceholder(),
  }) {
    return ChronicleActivateRequestBackfill(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
    );
  }
}

extension $ChronicleActivateRequestBackfillCopyWith
    on ChronicleActivateRequestBackfill {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChronicleActivateRequestBackfill.copyWith(...)` or `instanceOfChronicleActivateRequestBackfill.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChronicleActivateRequestBackfillCWProxy get copyWith =>
      _$ChronicleActivateRequestBackfillCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChronicleActivateRequestBackfill _$ChronicleActivateRequestBackfillFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('ChronicleActivateRequestBackfill', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['from']);
      final val = ChronicleActivateRequestBackfill(
        from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$ChronicleActivateRequestBackfillToJson(
  ChronicleActivateRequestBackfill instance,
) => <String, dynamic>{'from': instance.from.toIso8601String()};
