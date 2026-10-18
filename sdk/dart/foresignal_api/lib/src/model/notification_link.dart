//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'notification_link.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NotificationLink {
  /// Returns a new [NotificationLink] instance.
  NotificationLink({

    required  this.label,

    required  this.url,
  });

  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;



  @JsonKey(
    
    name: r'url',
    required: true,
    includeIfNull: false,
  )


  final String url;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NotificationLink &&
      other.label == label &&
      other.url == url;

    @override
    int get hashCode =>
        label.hashCode +
        url.hashCode;

  factory NotificationLink.fromJson(Map<String, dynamic> json) => _$NotificationLinkFromJson(json);

  Map<String, dynamic> toJson() => _$NotificationLinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

