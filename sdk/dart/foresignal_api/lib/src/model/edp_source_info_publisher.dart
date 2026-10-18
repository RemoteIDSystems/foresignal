//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_source_info_publisher.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpSourceInfoPublisher {
  /// Returns a new [EdpSourceInfoPublisher] instance.
  EdpSourceInfoPublisher({

    required  this.name,

     this.url,

     this.contact,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'contact',
    required: false,
    includeIfNull: false,
  )


  final String? contact;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpSourceInfoPublisher &&
      other.name == name &&
      other.url == url &&
      other.contact == contact;

    @override
    int get hashCode =>
        name.hashCode +
        (url == null ? 0 : url.hashCode) +
        (contact == null ? 0 : contact.hashCode);

  factory EdpSourceInfoPublisher.fromJson(Map<String, dynamic> json) => _$EdpSourceInfoPublisherFromJson(json);

  Map<String, dynamic> toJson() => _$EdpSourceInfoPublisherToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

