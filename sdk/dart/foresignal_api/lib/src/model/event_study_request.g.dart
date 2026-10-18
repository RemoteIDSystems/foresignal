// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_study_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventStudyRequestCWProxy {
  EventStudyRequest name(String? name);

  EventStudyRequest selector(EventSelector selector);

  EventStudyRequest target(String target);

  EventStudyRequest from(DateTime from);

  EventStudyRequest to(DateTime to);

  EventStudyRequest asOf(DateTime? asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  EventStudyRequest call({
    String? name,
    EventSelector selector,
    String target,
    DateTime from,
    DateTime to,
    DateTime? asOf,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventStudyRequest.copyWith(...)` or call `instanceOfEventStudyRequest.copyWith.fieldName(value)` for a single field.
class _$EventStudyRequestCWProxyImpl implements _$EventStudyRequestCWProxy {
  const _$EventStudyRequestCWProxyImpl(this._value);

  final EventStudyRequest _value;

  @override
  EventStudyRequest name(String? name) => call(name: name);

  @override
  EventStudyRequest selector(EventSelector selector) =>
      call(selector: selector);

  @override
  EventStudyRequest target(String target) => call(target: target);

  @override
  EventStudyRequest from(DateTime from) => call(from: from);

  @override
  EventStudyRequest to(DateTime to) => call(to: to);

  @override
  EventStudyRequest asOf(DateTime? asOf) => call(asOf: asOf);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventStudyRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventStudyRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventStudyRequest call({
    Object? name = const $CopyWithPlaceholder(),
    Object? selector = const $CopyWithPlaceholder(),
    Object? target = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
  }) {
    return EventStudyRequest(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      selector: selector == const $CopyWithPlaceholder() || selector == null
          ? _value.selector
          // ignore: cast_nullable_to_non_nullable
          : selector as EventSelector,
      target: target == const $CopyWithPlaceholder() || target == null
          ? _value.target
          // ignore: cast_nullable_to_non_nullable
          : target as String,
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
    );
  }
}

extension $EventStudyRequestCopyWith on EventStudyRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventStudyRequest.copyWith(...)` or `instanceOfEventStudyRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventStudyRequestCWProxy get copyWith =>
      _$EventStudyRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventStudyRequest _$EventStudyRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('EventStudyRequest', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['selector', 'target', 'from', 'to'],
      );
      final val = EventStudyRequest(
        name: $checkedConvert('name', (v) => v as String?),
        selector: $checkedConvert(
          'selector',
          (v) => EventSelector.fromJson(v as Map<String, dynamic>),
        ),
        target: $checkedConvert('target', (v) => v as String),
        from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
        to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$EventStudyRequestToJson(EventStudyRequest instance) =>
    <String, dynamic>{
      'name': ?instance.name,
      'selector': instance.selector.toJson(),
      'target': instance.target,
      'from': instance.from.toIso8601String(),
      'to': instance.to.toIso8601String(),
      'asOf': ?instance.asOf?.toIso8601String(),
    };
