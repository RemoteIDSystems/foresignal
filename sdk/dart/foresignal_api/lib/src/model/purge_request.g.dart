// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'purge_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PurgeRequestCWProxy {
  PurgeRequest purgeAlerts(bool? purgeAlerts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PurgeRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PurgeRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  PurgeRequest call({bool? purgeAlerts});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPurgeRequest.copyWith(...)` or call `instanceOfPurgeRequest.copyWith.fieldName(value)` for a single field.
class _$PurgeRequestCWProxyImpl implements _$PurgeRequestCWProxy {
  const _$PurgeRequestCWProxyImpl(this._value);

  final PurgeRequest _value;

  @override
  PurgeRequest purgeAlerts(bool? purgeAlerts) => call(purgeAlerts: purgeAlerts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PurgeRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PurgeRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PurgeRequest call({Object? purgeAlerts = const $CopyWithPlaceholder()}) {
    return PurgeRequest(
      purgeAlerts: purgeAlerts == const $CopyWithPlaceholder()
          ? _value.purgeAlerts
          // ignore: cast_nullable_to_non_nullable
          : purgeAlerts as bool?,
    );
  }
}

extension $PurgeRequestCopyWith on PurgeRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPurgeRequest.copyWith(...)` or `instanceOfPurgeRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PurgeRequestCWProxy get copyWith => _$PurgeRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PurgeRequest _$PurgeRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PurgeRequest', json, ($checkedConvert) {
      final val = PurgeRequest(
        purgeAlerts: $checkedConvert('purgeAlerts', (v) => v as bool? ?? false),
      );
      return val;
    });

Map<String, dynamic> _$PurgeRequestToJson(PurgeRequest instance) =>
    <String, dynamic>{'purgeAlerts': ?instance.purgeAlerts};
